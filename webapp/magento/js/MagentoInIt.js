jQuery(function() {
    jQuery.fn.form = function() {
        var form_id = jQuery(this).attr('form');
        if (form_id === undefined) {
            return jQuery(this).closest('form');
        } else {
            return jQuery('#' + form_id);
        }
    }
    
    jQuery.fn.fields = function() {
        var id = jQuery(this).attr('id');
        if (id == undefined) {
            return jQuery(this).find(':input:not([form])');
        } else {
            return jQuery.merge(jQuery(this).find(':input:not([form]), :input[form=' + id + ']'), jQuery(':input[form=' + id + ']'))
        }
    }

    jQuery.validator.setDefaults({
        // Note: Here we are allowing chosen-select to run validation as by default select or input having either of this classes marked as hidden.
        ignore: ':hidden:not(.chosen-select)'
    });

    jQuery.validator.addMethod('req', function(v, e, p) {
        switch(e.nodeName.toLowerCase()) {
            case 'select':
                var val = jQuery(e).val();
                return val && val.length > 0;
            case 'input':
                if(this.checkable(e)) {
                    return this.getLength(v, e) > 0;
                }
            default:
                return jQuery.trim(v).length > 0;
            }
        },
        function(v, e) {
            var label_text = getValidateElementLabel(v, e);
            return 'Please enter ' + label_text;
        }
    );

    jQuery.validator.addMethod('phone', function(v, e, p) {
        return /^(\d{0,3}[ .-]?\d{3}[ .-]?\d{3}[ .-]?\d{4})?$/.test(v); 
        },
        'Please enter a valid phone number. Example: 123-123-123-1234.'
    );
    jQuery.validator.addMethod('usCanadaZip', function(v, e, p) {
            var form = jQuery(e).closest('form'),
                country_box = jQuery(e).data("country-box"),
                country_name = jQuery(country_box).val();
            if ((jQuery(form).find('.countryGeoId').val() === "USA") || (country_name === "USA")) {
                return (this.getLength(jQuery.trim(v), e) <= 5 && (/^[0-9]{5}$/).test(v));
            } else if ((jQuery(form).find('.countryGeoId').val() === "CAN") || (country_name === "CAN")) {
                return (this.getLength(jQuery.trim(v), e) <= 7 && (/^[A-z][0-9][A-z][ .-]?[0-9][A-z][0-9]$/).test(v));
                /* TODO: For now we are validating to true all zip codes for countries other than US and Canada. Validations for other countries will be added on requirement*/ 
            } else {
                return (/^[A-Za-z0-9]?[A-Za-z0-9 ]*$/).test(v);
            }
        },
        'Please enter a valid zip code.'
    );
    function getValidateElementLabel(v, e) {
        jQuery(e).siblings('label[class="error"]').remove();
        var label_text = jQuery(e).data('label') || jQuery(e).siblings('label:first').text();
        // can't use the title attribute here, as the validation API uses it to override the whole message
        if (label_text === undefined || label_text === "") {
            label_text = jQuery(e).attr('name');
        }
        return label_text;
    }

    function showSpinner(elt) {
        if(elt.attr('target') !== '_blank') {
            jQuery('<div/>').addClass('ui-widget-overlay').css({
                height: jQuery(document).height() + 'px',
                width: jQuery(document).width() + 'px',
                'z-index': '1002'
            }).appendTo('body');
            jQuery('<i/>').addClass('fa fa-spinner fa-spin fa-4x').css({
                position: 'fixed',
                top: '50%', // approximate position
                left: '50%', // approximate position
                'z-index': '1002'
            }).appendTo('body');
        }
    }
    jQuery.validator.addClassRules({
        'required': {
            req: true
        },
        'validate-phone': {
            phone: true
        },
        'validate-usCanadaZip': {
            usCanadaZip: true
        }
    });

    initValidations = (function() {
        function setValidation(elt) {
            jQuery(elt).validate({
                errorPlacement: function(error, input_elt) {
                    var input_id = jQuery(input_elt).attr('id') || '',
                        msg_elt = '';
                    // This code is to support error placement in modal window, on page modal window rendering need to be fixed.
                    if(input_id !== undefined && input_id !== '') {
                        var modal_elt = input_elt.closest('.modal');
                        if(modal_elt.size() === 0) {
                            msg_elt = jQuery('#validate-' + input_id);
                        } else {
                            msg_elt = modal_elt.find('#validate-' + input_id);
                        }
                    }
                    // The element where validation message will be placed is identified by css selector #validate-<input field's name>
                    // If no such element exists, then the validation message will be appended to input field's parent element
                    if (jQuery(msg_elt).size() === 0) {
                        error.appendTo(input_elt.parent());
                    } else {
                        error.appendTo(msg_elt);
                    }
                }
            });
        }
        // Set validations on all the forms which have class requireValidation
        jQuery('form.requireValidation').each(function(i, elt) {
            setValidation(elt);
        });

        return function(elt) {
            jQuery(elt).find('form.requireValidation').each(function(i, elt) {
                setValidation(elt);
            });
        }
    }());
    jQuery('body').on('keyup change', '.chosen-select', function(){
        var elt = jQuery(this);
        if (elt.attr('value') === '') {
            elt.siblings('label.error').show();
        } else {
            elt.siblings('label.error').hide();
        }
    });

    function toggleDisplay(target, state, effect) {
        if (jQuery(target) !== undefined) {
            if (state === true) {
                if (effect === 'slide') {
                    jQuery(target).slideDown().removeClass('hide');
                } else if (effect === 'fade') {
                    jQuery(target).fadeIn().removeClass('hide');
                } else {
                    jQuery(target).show().removeClass('hide');
                }
            } else {
                if (effect === 'slide') {
                    jQuery(target).slideUp();
                } else if (effect === 'fade') {
                    jQuery(target).fadeOut();
                } else {
                    jQuery(target).hide();
                }
            }
        }
    }
    jQuery('body').on('change click', '[data-toggle-display]', function(e) {
        var effect = jQuery(this).data('toggle-effect');
        if (jQuery(this).is(':checkbox, :radio:checked')) {
            toggleDisplay(jQuery(this).data('toggle-display'), jQuery(this).is(':checked'), effect);
        } else {
            e.preventDefault();
            jQuery(jQuery(this).data('toggle-display')).each(function() {
                toggleDisplay(jQuery(this), !jQuery(this).is(':visible'), effect);
            });
        }
    });

    jQuery('body').on('change click', '[data-toggle-hide]', function(e) {
        var effect = jQuery(this).data('toggle-effect');
        if (jQuery(this).is(':checkbox, :radio:checked')) {
            toggleDisplay(jQuery(this).data('toggle-hide'), !jQuery(this).is(':checked'), effect);
        } else {
            e.preventDefault();
            jQuery(jQuery(this).data('toggle-hide')).each(function() {
                toggleDisplay(jQuery(this), !jQuery(this).is(':visible'), effect);
            });
        }
    });

    jQuery('body').on('change', ':input', function(e) {
        if (jQuery(this).attr('data-smart-change') === undefined) {
          var form = jQuery(this).form();
          if (form.hasClass('js-change-submit')) {
              form.trigger('submit', e);
          }
        }
    });

    jQuery('body').on('change', '[data-ajax-update]', function() {
        jQuery.proxy(ajaxUpdater, {elt: this})();
    });

    jQuery('body').on('click', 'a[data-ajax-update], button[data-ajax-update]', function(e) {
        e.preventDefault();
        jQuery.proxy(ajaxUpdater, {elt: this})();
    });

    initAjaxObservers = (function() {
        function setAjaxObserver(elt) {
            var elt = jQuery(elt),
                options = {
                    form_elt: elt,
                    event: elt.attr('data-submitMethod') || 'submit',
                    paramSource: elt.attr('data-paramSource') || '',
                    anim_method: elt.data('anim-method'),
                    anim_direction:elt.data('anim-direction'),
                    display_dialog_title: elt.attr('data-dialogTitle'),
                    display_success_method: elt.attr('data-successMethod'),
                    display_error_method: elt.attr('data-errorMethod'),
                    new_dialog_update: elt.attr('data-newDialogUpdate')
                };
            ajaxifyForm(options);
        }
        // Set AJAX observers on all the forms which have class ajaxMe
        jQuery('form.ajaxMe').each(function(i, elt) {
            setAjaxObserver(elt);
        });

        return function(elt) {
            jQuery(elt).find('form.ajaxMe').each(function(i, elt) {
                setAjaxObserver(elt);
            });
        };
    }());

    // this is a global variable
    default_modal_options = {
        replace: true
    }

    jQuery('body').on('click', '[data-dialog-href], .dialogWindow', function(e) {
        // the dialog's content, can be an on-page element or an URL, in which case we will need to ajax-load the data
        var href = jQuery(this).data('dialog-href'),
            param_source = jQuery(this).data('param-source') || '',
            parameters = jQuery(param_source).serialize(),
            id = jQuery(this).data('id'),
            title = jQuery(this).attr('title'),
            ajax_loader = jQuery('<i/>').addClass('dialog-ajax-loader fa fa-spinner fa-4x fa-spin'),
            modal_header = jQuery('<div/>').addClass('modal-header'),
            modal_title = jQuery('<h4/>').addClass('modal-title').html(title),
            modal_dismiss = jQuery('<button/>').addClass('close').attr('data-dismiss', 'modal').html('&times;'),
            modal_body = jQuery('<div/>').addClass('modal-body'),
            data_dialog_width = (jQuery(this).data('dialog-width')) ? jQuery(this).data('dialog-width') : "default";
        window.modal = jQuery('<div/>').addClass('modal '+data_dialog_width).attr('id', id);
        window.data_dialog_width = data_dialog_width;
        jQuery(window.modal).append(modal_header);
        jQuery(modal_header).append(modal_dismiss);
        jQuery(modal_header).append(modal_title);
        jQuery(modal_body).insertAfter(modal_header);
        jQuery(modal_body).append(ajax_loader);
        modal_header.attr('data-lookup-field', jQuery(this).attr('data-lookup-field'));

        rebindContainer(jQuery(window.modal));
        e.preventDefault();

        // adding backward compatibility
        if (!href && jQuery(this).hasClass('dialogWindow')) {
            href = jQuery(this).attr('href') || jQuery(this).attr('data-action');
            title = jQuery(this).attr('title');
        }

        if (href !== undefined) {
            /* jQuery's css selector does a ton of things except just the element selection, so if we use jQuery('/control/main')
             * then instead of returning empty array, it throws an error as it tries to consider the passed argument as regular expression
             * so assuming that we will only use id or class selector for pointing dialog's content's location, the below code is written
             */
            if (href.charAt(0) === '#' || href.charAt(0) === '.') {
                // we already have the dialog's content on the page, so let's use it, the content will tell us the dialog's title and the modal configuration
                jQuery(ajax_loader).remove();
                jQuery(modal_body).append(jQuery(href).html());
                if (title === undefined) {
                    modal_title.html(jQuery(href).find('.js-dialogTitle:first').text());
                }
                modal_body.find('.js-dialogTitle:first').hide();
                jQuery(window.modal).modal(default_modal_options);
                rebindContainer(jQuery(window.modal));
            } else {
                jQuery.ajax({
                    url: href,
                    data: parameters,
                    dataTypeString: "html",
                    beforeSend: function() {
                        jQuery(window.modal).modal(default_modal_options);
                    },
                    complete: function(xhr, status) {
                        /* Here we are hiding existing modal as content before ajax request is only ajax-loader. So in case size of content 
                         * after ajax request is enough for modal overflow, modal is not coming at appropriate place (It is coming at center 
                         * of the window). A new modal will appear whose positioning will be according to the size of content of ajax response.
                         */
                        jQuery(window.modal).modal('hide');
                        var response_without_scripts = jQuery(xhr.responseText).not('script'),
                            response_scripts = jQuery(xhr.responseText).filter('script'),
                            response_html = jQuery('<div/>').html(response_without_scripts);
                        if (title === undefined) {
                            modal_title.html(response_html.find('.js-dialogTitle:first').text());
                        }
                        jQuery(modal_body).html(response_html.html());
                        modal_body.find('.js-dialogTitle:first').hide();
                        jQuery(response_scripts).appendTo(window.modal);
                        jQuery(window.modal).modal(default_modal_options);
                        rebindContainer(jQuery(window.modal));
                    }
                });
            }
        }
    });
    jQuery('body').on('click', 'a', function(event) {
        var link_href = jQuery(this).attr('href');
        if(!(event.isDefaultPrevented()) && !(link_href === undefined || link_href === "" || link_href === 'javascript:void();' )) {
            if(link_href.indexOf('#') !== 0) {
                if (!(event.metaKey || event.ctrlKey || event.shiftKey)) {
                showSpinner(jQuery(this));
                }
            }
        }
    });
    jQuery('body').on('submit', 'form', function(event) {
        if(!event.isDefaultPrevented()) {
            jQuery(':focus').blur();
            showSpinner(jQuery(this));
        }
    });
    rebindContainer();
});
function ajaxifyForm(ext_options) {
    var form_elt = ext_options.form_elt,
        options = {
            form_elt: null,
            event: 'submit',
            parameters: '',
            paramSource: '',
            display_success_method: [],
            display_error_method: [],
            new_dialog_update: undefined,
            callback: jQuery.noop
        };
    jQuery.extend(options, ext_options || {});
    if (jQuery(form_elt).fields().is(':file')) {
        var ajaxLoaderElt = (form_elt.find('.ajax-loader').size() > 0) ? form_elt.find('.ajax-loader') : jQuery(form_elt.data('ajax-loader')) ;
        jQuery(form_elt).ajaxForm({
            beforeSend: function() {
                jQuery(ajaxLoaderElt).show();
            },
            complete: jQuery.proxy(function(xhr, status) {
                jQuery(ajaxLoaderElt).hide();
                handleAjaxResponse(jQuery.extend(this, {
                    response: status,
                    xhr: xhr
                }));
                this.callback(xhr.responseText, xhr);
            }, options)
        });
    } else {
        jQuery(options.form_elt).unbind(options.event);
        jQuery(options.form_elt).bind(options.event, jQuery.proxy(doAjaxTransaction, options));
    }
}
function doAjaxTransaction(event, event_orig) {
    var form_elt = jQuery(this.form_elt),
        parameters = form_elt.serialize() + ((this.parameters !== '') ? '&' + this.parameters : '') + ((jQuery(this.paramSource) !== '') ? '&' + jQuery(this.paramSource).serialize() : '');
    event.preventDefault();

    if (form_elt.valid()) {
        jQuery.ajax({
            url: form_elt.attr('action'),
            async: true,
            data: parameters,
            type: form_elt.attr('method'),
            beforeSend: function() {
                form_elt.fields().filter('button, [type=submit]').attr('disabled', 'disabled');
                if (event_orig !== undefined) {
                    if (jQuery(event_orig.target).siblings('.dynamic-ajax-loader').size() > 0) {
                        jQuery(event_orig.target).siblings('.dynamic-ajax-loader').addClass('ajax-loader abs').show();
                    } else if (jQuery(event_orig.target).parent().hasClass('relative')) {
                        jQuery(event_orig.target).parent().append(jQuery('<i/>').addClass('dynamic-ajax-loader ajax-loader abs'));
                    } else {
                        jQuery(event_orig.target).parent().append(jQuery('<i/>').addClass('dynamic-ajax-loader ajax-loader'));
                    }
                } else {
                    var ajaxLoaderElt = (form_elt.find('.ajax-loader').size() > 0) ? form_elt.find('.ajax-loader') : jQuery(form_elt.data('ajax-loader')) ;
                        jQuery(ajaxLoaderElt).show();
                }
            },
            complete: jQuery.proxy(function(xhr, status) {
                form_elt.fields().filter('button, [type=submit]').removeAttr('disabled');
                if (event_orig !== undefined) {
                    jQuery(event_orig.target).siblings('.dynamic-ajax-loader').removeClass('ajax-loader abs');
                } else {
                    var ajaxLoaderElt = (form_elt.find('.ajax-loader').size() > 0) ? form_elt.find('.ajax-loader') : jQuery(form_elt.data('ajax-loader')) ;
                        jQuery(ajaxLoaderElt).hide();
                }
                handleAjaxResponse(jQuery.extend(this, {
                    response: status,
                    xhr: xhr
                }));
                this.callback(event, xhr.responseText, xhr);
            }, this)
        });
    } else {
        this.callback(event);
    }
    return false;
}
function ajaxUpdater() {
    var options = this,
        elt = jQuery(options.elt),
        callback = options.callback ? options.callback : jQuery.noop;
        beforeSendCallback = options.beforeSendCallback ? options.beforeSendCallback : jQuery.noop;
        url = elt.data('update-url'),
        to_update = jQuery(elt.data('ajax-update')),
        valid = true,
        param_source = jQuery(elt.data('param-source')),
        form_fields = jQuery.unique(jQuery.merge(param_source.find(':input').andSelf(), elt.find(':input').andSelf()).filter(':input')),
        data = form_fields.serializeArray();
    form_fields.filter(':visible').each(function() {
        var validator = jQuery(this).form().data('validator');
        if(validator !== undefined) {
            valid = valid && (validator.check(this) == false ? false : true);
        }
    });
    if (valid) {
        jQuery.ajax({
            async: true,
            type: 'post',
            url: url,
            data: data,
            beforeSend: function(xhr, settings) {
                beforeSendCallback({
                    xhr: xhr,
                });
            },
            complete: function(xhr, status) {
                handleAjaxResponse({
                    xhr: xhr,
                    response: status,
                    display_success_method: to_update,
                    display_error_method: to_update,
                    status: status
                });
                callback(xhr.responseText, xhr);
            }
        });
    }
}

function handleAjaxResponse(options) {
    var response = options.response,
        xhr = options.xhr,
        data = jQuery(xhr.responseText).not('script').not('.messages'),
        scripts = jQuery(xhr.responseText).filter('script'),
        notification_messages = jQuery(xhr.responseText).filter('.messages').children(),
        to_update_selector = (response === 'success') ? options.display_success_method : options.display_error_method,
        to_update = jQuery(to_update_selector),
        default_dialog_title = (response === 'success') ? 'Notification' : 'Error',
        anim_method = (response === 'success') ? options.anim_method : '',
        anim_direction = (response === 'success' && options.anim_direction) ? options.anim_direction : '',
        new_dialog_title = (options.display_dialog_title) ? options.display_dialog_title : jQuery(window.modal).find('.modal-title'),
        new_dialog_update = options.new_dialog_update,
        data_dialog_width = (window.data_dialog_width) ? window.data_dialog_width : "default";
        if(jQuery(to_update_selector).find('.js-thfloat-foot').size() > 0){
            jQuery('.thfloat-table').remove();
        }
        if(new_dialog_title === undefined) {
            new_dialog_title = default_dialog_title;
        }
    // Check if we are supposed to redirect the user to a new page
    if (xhr.getResponseHeader('requestAction')) {
        window.location = xhr.getResponseHeader('requestAction');
        return;
    }
    if (data.size() > 0) {
        var focus_elt = jQuery(':focus'),
        focus_elt_id = focus_elt.attr('id');

        if (to_update.size() === 0) {
            // Elements to be updated are not specified, so we will use dialog to display the result
            var title = new_dialog_title,
                modal_dialog = jQuery('<div/>').addClass('modal-dialog'),
                modal_header = jQuery('<div/>').addClass('modal-header'),
                modal_title = jQuery('<h4/>').addClass('modal-title').html(title),
                modal_dismiss = jQuery('<button/>').addClass('close').attr('data-dismiss', 'modal').html('&times;'),
                modal_body = jQuery('<div/>').addClass('modal-body');

            window.modal = jQuery('<div/>').addClass('modal '+data_dialog_width);
            jQuery(window.modal).append(modal_header);
            jQuery(modal_header).append(modal_dismiss);
            jQuery(modal_header).append(modal_title);
            jQuery(modal_body).insertAfter(modal_header);
            jQuery(modal_body).append(data);
            jQuery(window.modal).modal(default_modal_options);
            rebindContainer(jQuery(window.modal));

        } else if (to_update.size() === 1 && new_dialog_update === undefined) {
                // We have exactly one element on the page to drop the data received
                if (anim_method === undefined) {
                    if (data.filter(to_update_selector).size() > 0) {
                        data = data.html();
                    }
                    jQuery(to_update).html(data);
                }
                else {
                    updateWithAnimation(to_update, data, anim_method, anim_direction);
                }
                // Close any open dialogs, so that user can see the results we just updated
                if (window.modal !== undefined) {
                    jQuery(window.modal).modal('hide');
                }
                rebindContainer(to_update);
                if (focus_elt_id !== undefined) {
                    var element = jQuery('#'+focus_elt_id);
                    element.focus().val(element.val());
                }
        } else {
                var isInDialog = false;
                // Below code would be used when we have a multiple section to update from data we have received.
                jQuery('<div/>').html(data).children().each(function(i, elt) {
                    var elt = jQuery(elt),
                        id = elt.attr('id'),
                        cls = (elt.attr('class') !== undefined) ? ('.' + elt.attr('class').split(' ').join('.')) : null;
                    if (id !== undefined) {
                        to_update.each(function(i, update_elt) {
                            if (jQuery(update_elt).attr('id') === id) {
                                jQuery(update_elt).html(elt.html());
                            }
                            if(jQuery('.modal').find('#'+id).size() > 0) {
                                isInDialog = true;
                            }
                        });
                        // Support for opening new dialog with multisection update.
                        if (new_dialog_update !== undefined && id === new_dialog_update) {
                            if(elt.find("#new-dialog-title").size() > 0) {
                                new_dialog_title = elt.find("#new-dialog-title").html();
                                elt.find("#new-dialog-title").remove();
                            }
                            createModal({title: new_dialog_title, content: elt, width: data_dialog_width});
                            rebindContainer(jQuery(window.modal));
                            isInDialog = true;
                        }
                    } else if (cls !== null) {
                        to_update.each(function(i, update_elt) {
                            if (jQuery.inArray(update_elt, jQuery(cls)) !== -1) {
                                jQuery(update_elt).html(elt.html());
                            }
                            if(jQuery('.modal').find(cls).size() > 0) {
                               isInDialog = true;
                         }
                        });
                    }
                });
            // Close any open dialogs, so that user can see the results we just updated
            if(!isInDialog && window.modal !== undefined) {
                jQuery(window.modal).modal('hide');
            }
            rebindContainer(to_update);
            if (focus_elt_id !== undefined) {
                jQuery('#'+focus_elt_id).focus();
            }
        }
        jQuery(scripts).appendTo('body');
    } else if (to_update.size() === 0 && window.modal !== undefined) {
        jQuery(window.modal).modal('hide');
    }
    jQuery(notification_messages).prependTo('#notification-messages');
}
function rebindContainer(elt) {
    if (elt === undefined) {
        elt = jQuery('body');
    }
    jQuery(".chosen-select").chosen();
    initValidations(elt)
    jQuery(elt).find('form div.form-group .required:input').each(function() {
        if (!jQuery(this).closest('div.form-group').find('span.asterisk').size() > 0) {
            jQuery(this).closest('div.form-group > div').children('label').append('<span class="asterisk"> *</span>');
        }
    });
    jQuery('body').find('[data-dependent]').each(function() {
        var parent_elt = jQuery(this),
            child_elt = jQuery(parent_elt.data('dependent'));
        parent_elt.data('child-clone', child_elt.clone());
        jQuery('body').on('change', '[data-dependent]', function() {
            var parent_elt = jQuery(this),
                child_elt = jQuery(parent_elt.data('dependent')),
                child_clone = parent_elt.data('child-clone'),
                selected_title = jQuery(parent_elt).find(':selected').attr('title');
            jQuery(child_elt).empty();
            child_clone.children().each(function() {
                if (jQuery(this).attr('value') === '') {
                    jQuery(child_elt).append(jQuery(this).clone());
                }
                if (selected_title !== '' && (jQuery(this).attr('label') === selected_title || jQuery(this).hasClass(selected_title))) {
                    jQuery(child_elt).append(jQuery(this).clone());
                }
            });
        });
        jQuery(this).change();
    });
    
    /* Use data-tooltip-content attribute for show text contents and data-tooltip-target attribute to show html contents */
    jQuery('body').on('mouseenter', '[data-tooltip-content], [data-tooltip-target]', function() {
        var scroll_offset = scrollOffset(jQuery(this)),
            window_height = jQuery(window).height(),
            window_width = jQuery(window).width(),
            direction_of_tooltip = (window_width > window_height) ? 'left-or-right' : 'top-or-bottom';
        if (jQuery(this).data('bs.popover') === undefined) {
            jQuery(this).popover({
                container : 'body',
                html : 'true',
                trigger: 'manual',
                title: jQuery(this).data('tooltip-title') || 'Information',
                content: jQuery(jQuery(this).data('tooltip-target')).html() || jQuery(this).data('tooltip-content')
            });
        }

        // fail safe code for cases when the tooltip is too close to the boundary of the viewport
        // change the direction of the tooltip in such cases
        jQuery(this).popover('show');
        if (direction_of_tooltip === 'left-or-right') {
            var half_tooltip_height = jQuery(this).data('bs.popover').$tip.height() / 2;
            if (scroll_offset.top < half_tooltip_height || (window_height - scroll_offset.top) < half_tooltip_height) {
                direction_of_tooltip = 'top-or-bottom';
            }
        } else {
            var half_tooltip_width = jQuery(this).data('bs.popover').$tip.width() / 2;
            if (scroll_offset.left < half_tooltip_width || (window_width - scroll_offset.left) < half_tooltip_width) {
                direction_of_tooltip = 'left-or-right';
            }
        }

        if (direction_of_tooltip === 'left-or-right') {
            if (scroll_offset.left > (window_width / 2)) {
                jQuery(this).data('bs.popover').options.placement = 'left'
            } else {
                jQuery(this).data('bs.popover').options.placement = 'right'
            }
        } else {
            if (scroll_offset.top > (window_height / 2)) {
                jQuery(this).data('bs.popover').options.placement = 'top'
            } else {
                jQuery(this).data('bs.popover').options.placement = 'bottom'
            }
        }
        jQuery(this).popover('show');
    });
    jQuery('body').on('mouseleave', '[data-tooltip-content], [data-tooltip-target]', function() {
        if (jQuery(this).data('bs.popover') !== undefined) {
            jQuery(this).popover('hide');
        }
    });
    
    function scrollOffset(elt) {
        var valueT = 0,
            valueL = 0,
            element = jQuery(elt).get(0);
        if (element !== undefined) {
            do {
                valueT += element.offsetTop || 0;
                valueL += element.offsetLeft || 0;
                // Safari fix
                if (element.offsetParent == document.body && jQuery(element).css('position') == 'absolute') {
                    break;
                }
            } while (element = element.offsetParent);

            element = jQuery(elt).get(0);
            do {
                if (!window.opera || element.tagName == 'BODY') {
                    valueT -= element.scrollTop || 0;
                    valueL -= element.scrollLeft || 0;
                }
            } while (element = element.parentNode);

            return {
                left: valueL,
                top: valueT
            };
        }
    }
    initAjaxObservers(elt);
}